FROM docker.io/python:slim

RUN useradd -rm -d /usr/src/app -s /bin/bash -u 1000 user
USER user

WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip install -r requirements.txt \
    && rm requirements.txt \
    && mkdir models
 
COPY api_server.py ./
COPY lil_analysis.py ./
COPY models/* ./models/

CMD [ "sh", "-c", "python ./api_server.py" ]
