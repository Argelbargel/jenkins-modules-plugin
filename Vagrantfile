# -*- mode: ruby -*-
# vi: set ft=ruby :

# use environment variables to customize
# set GITHUB_USER=<your username>
# set GITHUB_TOKEN=<your access-token>
GITHUB_USER = ENV['GITHUB_USER'] || nil
GITHUB_TOKEN = ENV['GITHUB_TOKEN'] || nil

KEY_FILE = ENV['KEY_FILE'] || 'deploy_key'
GENERATE_DEPLOY_KEY = <<-GENERATE_DEPLOY_KEY
#! /bin/bash
cd /vagrant
travis login --github-token $2
rm -f $3*
ssh-keygen -t rsa -b 4096 -f $3 -C "$1@users.noreply.github.com" -N ""
travis encrypt-file $3 --no-interactive
GENERATE_DEPLOY_KEY

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/xenial32"

  config.vm.provider "virtualbox" do |vb|
    vb.cpus = 1
    vb.memory = 512
  end

  config.vm.provision 'update', type: 'shell', inline: 'apt-get update -yy', privileged: true
  config.vm.provision 'install ruby', type: 'shell', inline: 'apt-get install -yy ruby ruby-dev libffi-dev', privileged: true
  config.vm.provision 'install travis', type: 'shell', inline: 'gem install travis -v 1.8.8 --no-rdoc -no-ri -n /usr/bin', privileged: true
  config.vm.provision 'generate key', run: 'always', type: 'shell', inline: GENERATE_DEPLOY_KEY, args: [GITHUB_USER, GITHUB_TOKEN, KEY_FILE], privileged: false
end
