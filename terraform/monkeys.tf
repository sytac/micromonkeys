resource "docker_container" "sucker" {
    image = "${docker_image.sucker.latest}"
    name  = "sucker"
    domainname = "sucker"
    ports = {
          internal = 8080
          external = 8080
    }
    command = ["java", "-Dmonkey=sucker", "-Dmonkey-id=1", "-Dmonkey-port=8080", "-Dmonkey-consul-endpoint=http://192.168.59.103:8500", "-jar", "sucker.jar"]
}

resource "docker_container" "grudger" {
    image = "${docker_image.grudger.latest}"
    name  = "grudger"
    domainname = "grudger"
    ports = {
          internal = 8080
          external = 8081
    }
    command = ["java", "-Dmonkey=grudger", "-Dmonkey-id=1", "-Dmonkey-port=8081", "-Dmonkey-consul-endpoint=http://192.168.59.103:8500", "-jar", "grudger.jar"]
}

resource "docker_container" "cheater" {
    image = "${docker_image.cheater.latest}"
    name  = "cheater"
    domainname = "cheater"
    ports = {
          internal = 8080
          external = 8082
    }
    command = ["java", "-Dmonkey=cheater", "-Dmonkey-id=1", "-Dmonkey-port=8082", "-Dmonkey-consul-endpoint=http://192.168.59.103:8500", "-jar", "cheater.jar"]
}

resource "docker_image" "sucker" {
    name = "skuro/monkey-sucker"
}

resource "docker_image" "grudger" {
    name = "skuro/monkey-grudger"
}

resource "docker_image" "cheater" {
    name = "skuro/monkey-cheater"
}