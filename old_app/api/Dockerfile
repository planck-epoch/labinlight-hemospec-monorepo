
FROM docker.io/ruby:2-slim AS build

RUN apt update && \
    apt install -y build-essential libpq-dev
RUN gem install bundler -v 2.4.22

WORKDIR /usr/app

COPY Gemfile ./
COPY Gemfile.lock ./
COPY Rakefile ./
USER root
RUN bundle config set --local without 'development test' && \
    bundle install 

COPY app ./app
COPY bin ./bin
COPY config ./config
COPY db ./db
COPY lib ./lib
COPY vendor ./vendor
COPY public ./public
COPY config.ru ./
COPY Gemfile ./
COPY Rakefile ./

FROM docker.io/ruby:2-slim 

RUN useradd -rm -d /usr/app -s /bin/bash -u 1000 user && \ 
    mkdir -p /usr/app && \ 
    chown user:user /usr/app

RUN apt update && \
    apt install -y libpq-dev && \
    rm -rf /var/lib/apt/lists/*

COPY entrypoint.sh /usr/bin/
COPY start.sh /usr/bin/
RUN chmod 555 /usr/bin/entrypoint.sh && \
    chmod 555 /usr/bin/start.sh

COPY --from=build  --chown=user:user /usr/app /usr/app
COPY --from=build /usr/local/bundle /usr/local/bundle

WORKDIR /usr/app

USER user

EXPOSE 3000

ENTRYPOINT ["/usr/bin/entrypoint.sh"]
CMD ["/usr/bin/start.sh"]
