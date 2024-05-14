from flask import Flask, request, jsonify
import json

# 导入推荐系统相关模块
from recommendation_system import RecommendationSystem

app = Flask(__name__)
recommendation_system = RecommendationSystem()


@app.route('/recommend', methods=['POST'])
def recommend():
    # 解析请求参数
    user_id = request.form.get('user_id')

    # 参数验证
    if not user_id:
        return jsonify({'error': 'User ID is required'}), 400

    try:
        user_id = int(user_id)
    except ValueError:
        return jsonify({'error': 'Invalid user ID'}), 400

    # 调用推荐系统
    recommendations = recommendation_system.get_recommendations(user_id)

    # 封装JSON数据并返回响应
    return jsonify({
        'user_id': user_id,
        'recommendations': recommendations
    })


if __name__ == '__main__':
    app.run()
