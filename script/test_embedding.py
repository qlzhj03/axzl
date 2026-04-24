from openai import OpenAI

# 初始化客户端，base_url 指向你的服务
client = OpenAI(
    api_key="dummy_key", # 可以随便填，你的服务没做鉴权的话
    base_url="http://localhost:8718/v1"
)

# 调用 Embeddings 接口
response = client.embeddings.create(
    model="bge-large-zh-v1.5",
    input="测试文本"
)

print(len(response.data[0].embedding))