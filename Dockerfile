FROM debian:stretch
RUN apt-get -qq -y update 
RUN apt-get install -y openscad make

COPY ./ /root/hall-of-mirrors

WORKDIR /root/hall-of-mirrors

CMD make all
