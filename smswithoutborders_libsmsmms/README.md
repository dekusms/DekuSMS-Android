
### Publishing on Maven

- In case no secring.gpg - generate one (read up steps to do so)
- In case secring.gp import into gpg
  - ```bash gpg --import secring.gpg```
- Publish key on keystore ubuntu
  - ```bash gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID_HERE```
- Now publish the package to maven
  - ```bash ./gradlew :smswithoutborders_libsmsmms:publish```
