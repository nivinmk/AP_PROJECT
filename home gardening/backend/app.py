from flask import Flask, jsonify, request

from ml_logic import predict_survival, recommend_plants

app = Flask(__name__)


@app.route("/recommend", methods=["POST"])
def recommend() -> tuple:
    data = request.get_json(silent=True) or {}

    try:
        water = data["water"]
        space = data["space"]
        sunlight = data["sunlight"]
    except KeyError as exc:
        return jsonify({"error": f"Missing field: {exc.args[0]}"}), 400

    try:
        plants = recommend_plants(water=water, space=space, sunlight=sunlight)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400

    return jsonify({"recommended_plants": plants}), 200


@app.route("/predict", methods=["POST"])
def predict() -> tuple:
    data = request.get_json(silent=True) or {}

    try:
        plant = data["plant"]
        water = data["water"]
        space = data["space"]
        sunlight = data["sunlight"]
    except KeyError as exc:
        return jsonify({"error": f"Missing field: {exc.args[0]}"}), 400

    try:
        result = predict_survival(plant=plant, water=water, space=space, sunlight=sunlight)
    except ValueError as exc:
        message = str(exc)
        status = 404 if "not found" in message.lower() else 400
        return jsonify({"error": message}), status

    return jsonify(result), 200


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=True)
