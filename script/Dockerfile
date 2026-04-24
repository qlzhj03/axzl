FROM python:3.10-slim

WORKDIR /app

# 关键：国内镜像源
ENV HF_ENDPOINT=https://hf-mirror.com
ENV PIP_ROOT_USER_ACTION=ignore
ENV PIP_NO_CACHE_DIR=1

# 复制依赖
COPY requirements.txt .

# pip 清华源加速安装
RUN pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 复制代码
COPY main.py .

# 暴露端口
EXPOSE 8718

# 启动服务
CMD ["python", "main.py"]