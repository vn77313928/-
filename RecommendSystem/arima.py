import pandas as pd
from statsmodels.tsa.arima.model import ARIMA


sales_data = {
    '销售时间': ['2022-01-01', '2022-02-01', '2022-03-01', '2022-04-01', '2022-05-01'],
    '销售商品类别': ['A', 'B', 'C', 'A', 'B'],
    '销售金额': [1000, 1200, 800, 1100, 1300]
}

# 将字典转换为DataFrame对象
df = pd.DataFrame(sales_data)

# 将销售时间列转换为时间类型
df['销售时间'] = pd.to_datetime(df['销售时间'])

# 将销售时间列设置为索引
df.set_index('销售时间', inplace=True)

# 将销售商品类别作为列，使用pivot函数进行透视
df_pivot = df.pivot(columns='销售商品类别', values='销售金额')

# 对缺失值进行填充，假设缺失值用0填充
df_pivot.fillna(0, inplace=True)

# 拟合ARIMA模型并进行预测
predictions = {}
for category in df_pivot.columns:
    model = ARIMA(df_pivot[category], order=(1, 0, 1))  # 使用ARIMA(1,0,1)模型，也可以根据需要选择合适的阶数
    results = model.fit()
    forecast = results.forecast(steps=3)  # 预测未来3个时间步长的数据
    predictions[category] = forecast

# 打印预测结果
print("预测结果:")
for category, forecast in predictions.items():
    print(f"商品类别 {category} 的预测销售金额:", forecast)