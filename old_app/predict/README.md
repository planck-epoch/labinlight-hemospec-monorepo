# Requirements 

This is relevant for non-Docker executions only

- python v3
- Install everything else using pip

(_if python v2 is also installed you may need to use pip3 instead and python3 for the remainder of this README_)
```sh
pip install -r requirements.txt
```

# Test
## "Unit Test"
A simple unit test is available 
```sh
python test.py
```

## API server
Run server, will open port 8080 (make sure it's available and bear in mind it listens to 0.0.0.0)
```sh
python api_server.py
```

Test it using the sample payload at sample-inputs/payload.json
```sh
curl -X POST -H "Content-Type: application/json" -d @./sample-inputs/payload.json http://localhost:8080/analyze
```