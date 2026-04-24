import os
import time
import uuid
from contextlib import asynccontextmanager
from typing import List, Union, Optional

import torch
import torch.nn.functional as F
import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from transformers import AutoTokenizer, AutoModel

# -------------------------- 配置部分 --------------------------
# 建议将环境变量设置放在代码最前面，确保在导入 transformers 前生效
os.environ["HF_ENDPOINT"] = "https://hf-mirror.com"

MODEL_NAME = "BAAI/bge-large-zh-v1.5"
MODEL_DEPLOYED_NAME = "bge-large-zh-v1.5"  # 对外暴露的模型名称

tokenizer = None
model = None
device = "cuda" if torch.cuda.is_available() else "cpu"


# -------------------------- OpenAI 格式数据模型 --------------------------

class EmbeddingRequest(BaseModel):
    model: str = Field(..., description="模型名称")
    input: Union[str, List[str]] = Field(..., description="输入文本，支持单字符串或字符串数组")
    encoding_format: Optional[str] = Field("float", description="编码格式，支持 float 或 base64")
    dimensions: Optional[int] = Field(None, description="输出向量维度（仅部分模型支持）")
    user: Optional[str] = Field(None, description="终端用户标识")


class EmbeddingObject(BaseModel):
    object: str = "embedding"
    embedding: List[float]
    index: int


class UsageInfo(BaseModel):
    prompt_tokens: int
    total_tokens: int


class EmbeddingResponse(BaseModel):
    object: str = "list"
    data: List[EmbeddingObject]
    model: str
    usage: UsageInfo
    id: str
    created: int


# -------------------------- 生命周期管理 --------------------------

@asynccontextmanager
async def lifespan(app: FastAPI):
    global tokenizer, model
    print(f"正在加载模型: {MODEL_NAME} (设备: {device})")

    try:
        # 移除 mirror 参数，改用环境变量 HF_ENDPOINT，这是更标准的做法
        tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
        model = AutoModel.from_pretrained(MODEL_NAME).to(device)
        model.eval()
        print("✅ 模型加载完成！")
    except Exception as e:
        print(f"❌ 模型加载失败: {e}")
        raise e

    yield
    # 清理资源
    del tokenizer, model
    if torch.cuda.is_available():
        torch.cuda.empty_cache()


# -------------------------- FastAPI 初始化 --------------------------

app = FastAPI(
    title="OpenAI Compatible Embedding Service",
    description="基于 BGE 模型的 OpenAI 格式向量化服务",
    version="1.0",
    lifespan=lifespan
)


# -------------------------- 核心逻辑函数 --------------------------

def count_tokens(texts: List[str]) -> int:
    """计算输入文本的总 Token 数"""
    total = 0
    for text in texts:
        # 使用 tokenizer 精确计算 token 数量
        total += len(tokenizer.encode(text, add_special_tokens=True))
    return total


def get_embedding(texts: List[str]) -> List[List[float]]:
    """获取向量（核心推理逻辑）"""
    encoded_input = tokenizer(
        texts,
        padding=True,
        truncation=True,
        max_length=512,
        return_tensors="pt"
    ).to(device)

    with torch.no_grad():
        model_output = model(**encoded_input)

    # BGE 标准取  token 向量并归一化
    sentence_embeddings = model_output[0][:, 0]
    sentence_embeddings = F.normalize(sentence_embeddings, p=2, dim=1)

    # 转换为 Python List
    return sentence_embeddings.cpu().numpy().tolist()


# -------------------------- OpenAI 兼容接口 --------------------------

@app.get("/")
@app.get("/health")
async def health():
    """健康检查"""
    return {"status": "ok", "model": MODEL_NAME, "device": device}


@app.get("/v1/models")
async def list_models():
    """兼容 OpenAI 的模型列表接口"""
    return {
        "object": "list",
        "data": [
            {
                "id": MODEL_DEPLOYED_NAME,
                "object": "model",
                "created": int(time.time()),
                "owned_by": "custom"
            }
        ]
    }


@app.post("/v1/embeddings", response_model=EmbeddingResponse)
async def create_embedding(request: EmbeddingRequest):
    """
    核心向量化接口，完全兼容 OpenAI 格式
    """
    # 1. 输入预处理
    if isinstance(request.input, str):
        input_texts = [request.input]
    else:
        input_texts = request.input

    if not input_texts:
        raise HTTPException(status_code=400, detail="Input text list cannot be empty")

    # 2. 推理获取向量
    try:
        embeddings = get_embedding(input_texts)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference error: {str(e)}")

    # 3. 处理 dimensions 参数 (可选的向量裁剪)
    if request.dimensions and request.dimensions > 0:
        if request.dimensions > len(embeddings[0]):
            raise HTTPException(status_code=400, detail="Dimensions cannot be larger than native embedding size")
        embeddings = [emb[:request.dimensions] for emb in embeddings]

    # 4. 构造 OpenAI 格式响应
    prompt_tokens = count_tokens(input_texts)

    response = EmbeddingResponse(
        id=f"embedding-{uuid.uuid4().hex}",
        object="list",
        created=int(time.time()),
        model=MODEL_DEPLOYED_NAME,
        usage=UsageInfo(
            prompt_tokens=prompt_tokens,
            total_tokens=prompt_tokens
        ),
        data=[
            EmbeddingObject(
                object="embedding",
                embedding=emb,
                index=i
            ) for i, emb in enumerate(embeddings)
        ]
    )

    return response


# -------------------------- 启动入口 --------------------------

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8718)