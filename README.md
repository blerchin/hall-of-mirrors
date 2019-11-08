TO set up raspi:

- follow [tutorial](http://www.raspberryconnect.com/projects/65-raspberrypi-hotspot-accesspoints/158-raspberry-pi-auto-wifi-hotspot-switch-direct-connection) to setup ad-hoc network
- add `10.0.0.5 hall-of-mirrors.home` to `/etc/hosts`
- route `:8000` to `:80` so we can run node as a user
```
sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8000
```
