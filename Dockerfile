FROM hseeberger/scala-sbt as builder
ARG PROJECT
WORKDIR /build
COPY project project
COPY build.sbt .
RUN sbt update
COPY . .
RUN sbt $PROJECT/universal:packageBin

FROM openjdk:8u181-jre-slim
ARG VERSION
ARG PROJECT
ENV version=$VERSION
COPY --from=builder /build/$PROJECT/target/universal/. .
RUN unzip -o ./mu-smart-home-$VERSION.zip
RUN chmod +x mu-smart-home-$VERSION/bin/mu-smart-home
ENTRYPOINT mu-smart-home-$version/bin/mu-smart-home
