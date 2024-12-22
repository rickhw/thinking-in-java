import uuid
from datetime import datetime
import random

# 生成不同地區的前綴
regions = ['us', 'eu', 'ap', 'sa', 'af', 'me', 'ca']
directions = ['north', 'south', 'east', 'west', 'central']
numbers = list(range(1, 21))  # 1-20的數字

# 創建SQL文件內容
sql_content = """
-- 插入1000筆region資料
"""

# 用於確保region_code唯一性
used_codes = set()

def generate_unique_region_code():
    while True:
        region = random.choice(regions)
        direction = random.choice(directions)
        number = random.choice(numbers)
        code = f"{region}-{direction}-{number}"
        if code not in used_codes:
            used_codes.add(code)
            return code

# 生成1000筆資料
insert_statements = []
for i in range(1000):
    # 生成唯一的UUID
    id = str(uuid.uuid4())
    
    # 生成唯一的region_code
    region_code = generate_unique_region_code()
    
    # 生成描述
    description = f"Region {region_code.upper()}"
    
    # 創建時間戳 (0-現在之間的隨機時間)
    creation_time = random.randint(0, int(datetime.now().timestamp()))
    
    # state值 (0或1)
    state = random.choice([0, 1])
    
    # 創建INSERT語句
    insert_statement = f"INSERT INTO `region` (`id`, `description`, `region_code`, `creation_time`, `state`) VALUES ('{id}', '{description}', '{region_code}', {creation_time}, {state});"
    insert_statements.append(insert_statement)

# 將所有INSERT語句合併成最終的SQL
sql_content += "\n".join(insert_statements) + "\n"

print(sql_content)