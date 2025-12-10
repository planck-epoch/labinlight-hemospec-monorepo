FROM docker.io/node:14.21-bullseye-slim AS build
RUN apt update && \
    apt install -y python2 build-essential && \
    rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY package*.json yarn.lock* ./
RUN yarn install
COPY src ./src
COPY public ./public
COPY *.js .*.js ./
ARG LIL_API_URL
ENV VUE_APP_API_URL=$LIL_API_URL
RUN  yarn build

FROM docker.io/nginx:stable
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
