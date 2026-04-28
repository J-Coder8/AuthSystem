from flask import Flask, request, jsonify
from PIL import Image
import base64
import io

app = Flask(__name__)


def _normalize_base64(data: str) -> str:
    if not data:
        return ""
    data = data.strip()
    if data.startswith("data:"):
        parts = data.split(",", 1)
        if len(parts) == 2:
            return parts[1].strip()
    if "," in data:
        return data.split(",", 1)[1].strip()
    return data


def _decode_image(base64_image: str) -> Image.Image:
    raw = base64.b64decode(_normalize_base64(base64_image))
    return Image.open(io.BytesIO(raw))


def _create_encoding(image: Image.Image) -> str:
    img = image.convert("L").resize((64, 64))
    pixels = img.load()

    features = []
    grid = 8
    cell_w = img.width // grid
    cell_h = img.height // grid

    for row in range(grid):
        for col in range(grid):
            total = 0
            count = 0
            for y in range(row * cell_h, min((row + 1) * cell_h, img.height)):
                for x in range(col * cell_w, min((col + 1) * cell_w, img.width)):
                    total += pixels[x, y]
                    count += 1
            avg = total / count if count else 0
            features.append(avg / 255.0)

    def symmetry(horizontal: bool) -> float:
        w, h = img.size
        total_diff = 0
        count = 0
        if horizontal:
            for y in range(h // 2):
                for x in range(w):
                    total_diff += abs(pixels[x, y] - pixels[x, h - 1 - y])
                    count += 1
        else:
            for y in range(h):
                for x in range(w // 2):
                    total_diff += abs(pixels[x, y] - pixels[w - 1 - x, y])
                    count += 1
        return 1.0 - (total_diff / count / 255.0) if count else 0.0

    features.append(symmetry(True))
    features.append(symmetry(False))

    center_x = img.width // 4
    center_y = img.height // 4
    center_w = img.width // 2
    center_h = img.height // 2

    center_total = 0
    center_count = 0
    for y in range(center_y, min(center_y + center_h, img.height)):
        for x in range(center_x, min(center_x + center_w, img.width)):
            center_total += pixels[x, y]
            center_count += 1

    center_avg = center_total / center_count if center_count else 0
    overall_avg = sum(img.getdata()) / (img.width * img.height)
    center_ratio = (center_avg / overall_avg) if overall_avg else 0.0
    features.append(center_ratio)

    return str(features)


@app.post("/encode")
def encode_face():
    data = request.get_json(silent=True) or {}
    base64_image = data.get("base64Image", "")

    if not base64_image:
        return jsonify({"error": "base64Image is required"}), 400

    try:
        image = _decode_image(base64_image)
        encoding = _create_encoding(image)
        return jsonify({"encoding": encoding})
    except Exception as exc:
        return jsonify({"error": str(exc)}), 400


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
