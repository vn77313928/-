import random
import time
import numpy as np
import requests
import json
from requests.exceptions import Timeout, ConnectionError, SSLError
from datetime import datetime

multi_apikey = True

with open('config.json', "r") as file:
    config = json.load(file)
headers = {"Authorization": f"Bearer {config['Authorization']}"}

headers_list = [{"Authorization": f"Bearer {api_key}"} for api_key in config['Authorizations']]
def get_header():
    if multi_apikey:
        return random.choice(headers_list)
    else:
        return headers

def get_text_embedding(text, max_retries = 20):
    API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large"
    retries = 0

    while retries < max_retries:
        try:
            response = requests.post(API_URL, headers=get_header(), json={
                "inputs": text,
            }, timeout=20)
            json_data = response.json()
            return json_data
        except Timeout:
            print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Timeout occurred while calling text embedding API, attempt {retries + 1}')
            retries += 1
        except SSLError:
            print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] SSL error occurred while calling text embedding API, attempt {retries + 1}')
            retries += 1
        except ConnectionError:
            print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Connection error occurred while calling text embedding API, attempt {retries + 1}')
            retries += 1


    print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Failed to get text embedding after {max_retries} attempts')
    return None

def get_image_label_score(image, max_retries=20):
    API_URL = "https://api-inference.huggingface.co/models/google/vit-base-patch16-224"
    retries = 0

    while retries < max_retries:
        try:
            response = requests.post(API_URL, headers=get_header(), data=image, timeout=20)
            json_data = response.json()
            return json_data
        except Timeout:
            print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Timeout occurred while calling image label score API, attempt {retries + 1}')
            retries += 1
        except SSLError:
            print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] SSL error occurred while calling image label score API, attempt {retries + 1}')
            retries += 1
        except ConnectionError:
            print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Connection error occurred while calling image label score API, attempt {retries + 1}')
            retries += 1

    print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Failed to get image label score after {max_retries} attempts')
    return None


def get_image_embedding(image):
    json_data = get_image_label_score(image)
    if json_data is None:
        return None
    emb = np.ndarray(shape=(1024))
    labels = []
    scores = []
    for row_data in json_data:
        labels.append(row_data['label'])
        scores.append(float(row_data['score']))
    labels_emb = None
    while labels_emb is None or isinstance(labels_emb, dict):
        print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Retrying to get text embedding, labels_emb is None or dict. ')
        labels_emb = get_text_embedding(labels)
    for i in range(len(labels_emb)):
        label_emb = labels_emb[i]
        label_emb = label_emb[0]
        label_emb = np.array(label_emb)
        # 按行进行sum
        label_emb = np.sum(label_emb, axis=0) / len(label_emb)
        emb += label_emb * scores[i]

    return emb

if __name__ == '__main__':
    image = open("test2.jpg", "rb").read()
    start_time = time.time()
    emb = get_image_embedding(image)
    if emb is not None:
        end_time = time.time()
        print(emb)
        print(end_time - start_time)
    else:
        print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Failed to get image embedding due to API timeout')
