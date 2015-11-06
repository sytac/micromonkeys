# Create a container
resource "docker_container" "consul" {
    image = "${docker_image.consul.latest}"
    name = "consul"
    domainname = "consul"
    ports = {
          internal = 8400
          external = 8400
    }
    ports = {
          internal = 8500
          external = 8500
    }
    ports = {
          internal = 53
          external = 8600
          protocol = "udp"
    }

    command = ["-server", "-bootstrap"]
}

resource "docker_image" "consul" {
    name = "progrium/consul"
}