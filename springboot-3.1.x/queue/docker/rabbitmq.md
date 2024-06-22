
## docker

## see: https://hub.docker.com/_/rabbitmq/

```bash
TS=$(date +"%Y%m%d")
APP_NAME="rabbitmq"
#APP_VER="3.10"
#APP_VER="3.11"  # 20230221
APP_VER="3.12-management-alpine"  # 20231021
APP_CONTAINER_NAME="${APP_NAME}-v${APP_VER}-${TS}"
APP_VOLUME_DATA="$HOME/docker/${APP_CONTAINER_NAME}/data"
APP_VOLUME_CONF="$HOME/docker/${APP_CONTAINER_NAME}/config"


docker run \
    --name ${APP_CONTAINER_NAME} \
    -p 5672:5672 \
    -p 15672:15672 \
    -e RABBITMQ_DEFAULT_USER=root \
    -e RABBITMQ_DEFAULT_PASS=medusa \
    -v ${APP_VOLUME_DATA}:/data/db \
    -v ${APP_VOLUME_CONF}:/data/configdb \
    --restart=unless-stopped \
    -d ${APP_NAME}:${APP_VER}

```

http://localhost:15672

```yaml
rabbitmq:
  image: rabbitmq:management
  ports:
    - "5672:5672"
    - "15672:15672"
```




