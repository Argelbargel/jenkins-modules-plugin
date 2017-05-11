# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/zesty64"

  config.vm.provider "virtualbox" do |vb|
    vb.cpus = 1
    vb.memory = 512
  end

  config.vm.provision 'update', type: 'shell', inline: 'apt-get update -yy', privileged: true
  config.vm.provision 'install ruby', type: 'shell', inline: 'apt-get install -yy ruby ruby-dev libffi-dev', privileged: true
  config.vm.provision 'install travis', type: 'shell', inline: 'gem install travis -v 1.8.8 --no-rdoc -no-ri', privileged: true
  config.vm.provision 'generate key', type: 'shell', inline: 'ssh-keygen -t rsa -b 4096 -f ./vagrant/deploy_key -C "argelbargel@@users.noreply.github.com" -N ""'
end
