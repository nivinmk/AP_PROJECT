# Flask ML Backend

## Expected `ml_logic.py` contract
Create or move `ml_logic.py` into this `backend` folder with these functions:

```python
def recommend_plants(water: str, space: str, sunlight: str) -> list[str]:
    ...

def predict_survival(plant: str, water: str, space: str, sunlight: str) -> float:
    ...
```

Inputs are lowercase `"low" | "medium" | "high"` and output is:
- `recommend_plants`: top plant names
- `predict_survival`: score in percentage

## Run backend

```powershell
cd "C:\Users\nivin\IdeaProjects\AP_PROJECT1\home gardening\backend"
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python app.py
```

Server runs at `http://127.0.0.1:5000`.

## API examples

```powershell
curl -X POST http://127.0.0.1:5000/recommend `
  -H "Content-Type: application/json" `
  -d "{\"water\":\"medium\",\"space\":\"low\",\"sunlight\":\"high\"}"
```

```powershell
curl -X POST http://127.0.0.1:5000/predict `
  -H "Content-Type: application/json" `
  -d "{\"plant\":\"Tomato\",\"water\":\"medium\",\"space\":\"low\",\"sunlight\":\"high\"}"
```
