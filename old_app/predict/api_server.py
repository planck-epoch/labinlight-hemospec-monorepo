from flask import Flask, request, jsonify
import lil_analysis
import logging

from logging.config import dictConfig

dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s in %(module)s: %(message)s',
    }},
    'handlers': {'wsgi': {
        'class': 'logging.StreamHandler',
        'formatter': 'default'
    }},
    'root': {
        'level': 'DEBUG',
        'handlers': ['wsgi']
    }
})

app = Flask(__name__)


@app.get("/health")
def health():
    return "ok"

@app.post("/analyze")
def analyze():
    if request.is_json:
        data = request.get_json()
        if 'Samples' in  data and isinstance(data['Samples'], list) and 'SerialNumber' in data:
            result = lil_analysis.analyze(data['Samples'], data['SerialNumber'])
            if isinstance(result, (int, float)): 
                logging.error("Failed processing result for %s ", data['SerialNumber'])
                return str(result), 500
            return result.to_json(), 200
    logging.error("Invalid request, no serial # or samples")
    return {"error": "Request data is invalid"}, 415


if __name__ == "__main__":
    from waitress import serve
    serve(app, host="0.0.0.0", port=8080)