
# Building & Pushing this code
1. Generate a Github access token that has container regsitry access

2. Login

```bash
echo $GITHUB_ACCESS_TOKEN | docker login ghcr.io -u USERNAME --password-stdin
```


3. Build

```bash
mvn install
docker build . -t ghcr.io/orbitalapi/demos-composing-apis --load    
docker push ghcr.io/orbitalapi/demos-composing-apis
```
