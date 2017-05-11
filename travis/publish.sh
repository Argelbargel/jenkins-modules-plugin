#!/usr/bin/env bash
echo "Publishing release $TRAVIS_TAG..."

REPO=`git config remote.origin.url`
ORIGIN=${REPO/https:\/\/github.com\//git@github.com:}

git clone ${REPO} build/deploy

cd build/deploy

git checkout ${PUBLISH_BRANCH} || (git checkout --orphan ${PUBLISH_BRANCH} && git rm -r -q -f .)
git config user.name "Travis CI"
git config user.email "$GITHUB_USER_NAME@users.noreply.github.com"
git config push.default simple

cp -r ../repo/* .

if [ -n "$(git status -s)" ]; then
    git add .
    git commit -m "release: $TRAVIS_TAG"

    ENCRYPTED_KEY_VAR="encrypted_${DEPLOY_KEY_ID}_key"
    ENCRYPTED_IV_VAR="encrypted_${DEPLOY_KEY_ID}_iv"
    ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
    ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}
    openssl aes-256-cbc -K ${ENCRYPTED_KEY} -iv ${ENCRYPTED_IV} -in ../../deploy_key.enc -out deploy_key -d

    chmod 600 deploy_key
    eval `ssh-agent -s`
    ssh-add deploy_key

    git push ${ORIGIN} ${PUBLISH_BRANCH}
fi