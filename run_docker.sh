docker rm fi_smp_server
docker volume create --name=fi_smp_server_volume
docker build --tag=fi_smp_server:latest --rm=true .
docker run \
  --name=fi_smp_server \
  --publish=9002:9002 \
  --volume=fi_smp_server_volume:/var/lib/fi_smp_server \
  fi_smp_server:latest
