## Building
For results, go to  https://cloud.codenameone.com/buildapp/index.html

## Rebuilding
Your certificates will expire, so you need to generate new ones.

### Expired Certs (Mac)
To renew Certificates: Go to CodenameOne Properties and click on the IOS Certificate Wizard. You don't need to generate push certificates. Do NOT follow directions at stack-overflow like this: https://stackoverflow.com/questions/2177143/how-to-renew-an-iphone-development-certificate

For Provisioning Profiles, background info: see https://customersupport.doubledutch.me/hc/en-us/articles/229496268-iOS-How-to-Create-a-Provisioning-Profile

But the certificate wizard will do all the work for you and install the files in the right directory. Make sure it generates both .p12 files and new provisioning profiles. Sometimes it misses one or two of them. If so, just run it again.

### Installing
To download the installer on your Android, you first need to close all windows in Chrome.

