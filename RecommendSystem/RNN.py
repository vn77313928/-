import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
 
# 读取数据
data = pd.read_csv('ratings.csv')
 
# 将用户和物品映射到整数索引
user_ids = data['user_id'].unique().tolist()
user2idx = {user_id: idx for idx, user_id in enumerate(user_ids)}
idx2user = {idx: user_id for idx, user_id in enumerate(user_ids)}
item_ids = data['item_id'].unique().tolist()
item2idx = {item_id: idx for idx, item_id in enumerate(item_ids)}
idx2item = {idx: item_id for idx, item_id in enumerate(item_ids)}
 
# 构建用户-物品序列数据
sequences = []
for _, row in data.iterrows():
    user_id = row['user_id']
    item_id = row['item_id']
    user_idx = user2idx[user_id]
    item_idx = item2idx[item_id]
    sequences.append((user_idx, item_idx))
 
# 划分序列数据为输入和目标
input_sequences = sequences[:-1]
target_sequences = sequences[1:]
 
# 定义数据集类
class SequenceDataset(Dataset):
    def __init__(self, sequences):
        self.sequences = sequences
        
    def __len__(self):
        return len(self.sequences)
    
    def __getitem__(self, index):
        user_idx, item_idx = self.sequences[index]
        return user_idx, item_idx
 
# 创建训练集和测试集数据加载器
train_ratio = 0.8
train_size = int(train_ratio * len(input_sequences))
train_data = SequenceDataset(input_sequences[:train_size])
train_loader = DataLoader(train_data, batch_size=32, shuffle=True)
test_data = SequenceDataset(input_sequences[train_size:])
test_loader = DataLoader(test_data, batch_size=32)
 
# 定义RNN模型
class RNNModel(nn.Module):
    def __init__(self, num_users, num_items, hidden_size):
        super(RNNModel, self).__init__()
        self.embedding_user = nn.Embedding(num_users, hidden_size)
        self.embedding_item = nn.Embedding(num_items, hidden_size)
        self.rnn = nn.GRU(hidden_size, hidden_size)
        self.fc = nn.Linear(hidden_size, num_items)
        
    def forward(self, user, item):
        user_embed = self.embedding_user(user)
        item_embed = self.embedding_item(item)
        output, _ = self.rnn(item_embed.unsqueeze(0))
        output = output.squeeze(0)
        logits = self.fc(output)
        return logits
 
# 创建RNN模型实例
num_users = len(user_ids)
num_items = len(item_ids)
hidden_size = 64
model = RNNModel(num_users, num_items, hidden_size)
 
# 定义损失函数和优化器
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=0.001)
 
# 训练模型
num_epochs = 10
for epoch in range(num_epochs):
    model.train()
    for user, item in train_loader:
        optimizer.zero_grad()
        logits = model(user, item)
        loss = criterion(logits, item)
        loss.backward()
        optimizer.step()
    print(f"Epoch [{epoch+1}/{num_epochs}], Loss: {loss.item()}")
 
# 测试模型
model.eval()
with torch.no_grad():
    for user, item in test_loader:
        logits = model(user, item)
        _, predicted = torch.max(logits, dim=1)
        for i in range(len(user)):
            user_idx = user[i].item()
            item_idx = predicted[i].item()
            user_id = idx2user[user_idx]
            item_id = idx2item[item_idx]
            print(f"用户 {user_id} 下一个可能喜欢的物品是 {item_id}")