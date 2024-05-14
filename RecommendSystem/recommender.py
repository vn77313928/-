import pickle
import faiss
import dbControler
import numpy as np
from embeder import get_text_embedding, get_image_embedding
from multiprocessing import Pool, cpu_count
from datetime import datetime
import time

dimention = 1024 * 2 + 64 + 64 + 32 + 32 # Bart * 2 + price + date + Owner + Likes

def replace_nans_infs_custom(matrix, nan_replace_value=0.0, inf_replace_value=-1e6):
    """
    用给定值替换矩阵中的 NaN 和 Inf 值，并返回它们的位置。
    :param matrix: 输入的 NumPy 二维矩阵。
    :param nan_replace_value: 用于替换 NaN 的数值，默认为 0.0。
    :param inf_replace_value: 用于替换 Inf 的数值，默认为 -1e9。
    :return: 处理后的矩阵和一个包含 NaN 和 Inf 位置的列表。
    """
    if not isinstance(matrix, np.ndarray):
        raise ValueError("Input must be a NumPy array")

    if len(matrix.shape) != 2:
        raise ValueError("Input must be a 2D array")

    # 找到 NaN 和 Inf 的位置
    nan_positions = np.argwhere(np.isnan(matrix))
    inf_positions = np.argwhere(np.isinf(matrix))

    # 分别替换 NaN 和 Inf
    matrix = np.where(np.isnan(matrix), nan_replace_value, matrix)
    matrix = np.where(np.isinf(matrix), inf_replace_value, matrix)

    # 合并位置列表并去重
    positions = np.vstack((nan_positions, inf_positions))
    unique_positions = np.unique(positions, axis=0)

    return matrix, unique_positions.tolist()



def process_data_row_wrapper(args):
    i, data_row = args
    return i, process_data_row(data_row)

def datetime_to_binary_array(dt):
    # 将datetime对象转换为POSIX时间戳（以秒为单位）
    timestamp = dt.timestamp()

    # 将时间戳转换为整数（以微秒为单位）
    int_timestamp = round(timestamp * 1e6)

    # 将整数转换为64位二进制字符串
    int64_bin = format(int_timestamp, '064b')

    # 将二进制字符串转换为0，1数组
    return np.array([int(bit) for bit in int64_bin])

def int32_to_binary_array(int32_number):
    int32_bin = format(int32_number & 0xFFFFFFFF, '032b')
    return np.array([int(bit) for bit in int32_bin])

def float_to_binary_array(float_number):
    float_bin = format(np.float64(float_number).view(np.int64), '064b')
    return np.array([int(bit) for bit in float_bin])

def process_data_row(data_row):
    '''
    将data_row转换成向量
    :param data_row:
    :return:
    '''
    pv_id, viewer_id, photo_owner_id, imgid, view_timestamp, imgname, story, stoneurl, price, likesnumber = data_row
    # 1. 将price转换成二进制
    price_array = float_to_binary_array(price)
    # 2. 将photo_owner_id转换成二进制
    photo_owner_id_array = int32_to_binary_array(photo_owner_id)
    # 3. 将likesnumber转换成二进制
    likesnumber_array = int32_to_binary_array(likesnumber)
    # 4. 将imgname和story转换成向量
    text = imgname + "@" + story
    embs = get_text_embedding(text)

    while embs is None or isinstance(embs, dict):
        print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Retry to get emb for {imgname}. None: {embs is None} | dict: {isinstance(embs, dict)}')
        print(embs)
        embs = get_text_embedding(text)
    embs = embs[0]
    text_emb = np.ndarray(shape=(1024))
    for emb in embs:
        text_emb += np.array(emb)

    # if embs is not None and not isinstance(embs, dict):
    #     embs = embs[0]
    #     text_emb = np.ndarray(shape=(1024))
    #     for emb in embs:
    #         text_emb += np.array(emb)
    # else:
    #     text_emb = np.zeros(shape=(1024))

    # 5. 将stoneurl读取为图片，然后获取向量
    stoneurl = "../web" + stoneurl
    image = open(stoneurl, "rb").read()
    image_emb = get_image_embedding(image)

    while image_emb is None:
        print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Retry to get embedding for {stoneurl}')
        image_emb = get_image_embedding(image)

    # if image_emb is None:
    #     image_emb = np.zeros(shape=(1024))

    # 6. 将view_timestamp转换成二进制
    view_timestamp_array = datetime_to_binary_array(view_timestamp)
    # 将所有的向量拼接起来
    vector = np.concatenate((image_emb, text_emb, price_array, view_timestamp_array, photo_owner_id_array, likesnumber_array))
    print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] Successfully Processed {imgname}')
    return vector


class Recommender:
    def get_pv_from_db(self):
        db = self.DBController
        # 从数据库中获取数据
        # 读取photo_view_records表里面的数据
        # +----------------+----------+------+-----+---------+----------------+
        # | Field          | Type     | Null | Key | Default | Extra          |
        # +----------------+----------+------+-----+---------+----------------+
        # | id             | int      | NO   | PRI | NULL    | auto_increment |
        # | viewer_id      | int      | YES  | MUL | NULL    |                |
        # | photo_owner_id | int      | NO   | MUL | NULL    |                |
        # | imgid          | int      | NO   | MUL | NULL    |                |
        # | view_timestamp | datetime | NO   |     | NULL    |                |
        # +----------------+----------+------+-----+---------+----------------+
        pv_records_columns = ["id", "viewer_id", "photo_owner_id", "imgid", "view_timestamp"]
        pv_records = db.query("photo_view_records", pv_records_columns)

        return pv_records

    def get_images_from_db(self):
        db = self.DBController
        # 读取images表里面的数据
        # +-------------+---------------+------+-----+---------+----------------+
        # | Field       | Type          | Null | Key | Default | Extra          |
        # +-------------+---------------+------+-----+---------+----------------+
        # | imgid       | int           | NO   | PRI | NULL    | auto_increment |
        # | uid         | int           | NO   |     | NULL    |                |
        # | orderid     | int           | YES  |     | NULL    |                |
        # | imgname     | varchar(45)   | NO   |     | NULL    |                |
        # | story       | varchar(1024) | YES  |     | NULL    |                |
        # | stoneurl    | varchar(512)  | YES  |     | NULL    |                |
        # | price       | double        | NO   |     | NULL    |                |
        # | uploaddate  | timestamp     | YES  |     | NULL    |                |
        # | likesnumber | int           | NO   |     | 0       |                |
        # | visible     | tinyint       | NO   |     | 1       |                |
        # | havesold    | tinyint       | NO   |     | 0       |                |
        # +-------------+---------------+------+-----+---------+----------------+
        images_columns = ["imgid", "uid", "orderid", "imgname", "story", "stoneurl", "price", "uploaddate",
                          "likesnumber", "visible", "havesold"]
        images = db.query("images", images_columns)

        return images

    def get_orders_from_db(self):
        db = self.DBController
        # 读取orders表里面的数据
        # +------------+----------------+------+-----+---------+----------------+
        # | Field      | Type           | Null | Key | Default | Extra          |
        # +------------+----------------+------+-----+---------+----------------+
        # | orderid    | int            | NO   | PRI | NULL    | auto_increment |
        # | sellerid   | int            | NO   |     | NULL    |                |
        # | buyerid    | int            | NO   |     | NULL    |                |
        # | imgid      | int            | NO   |     | NULL    |                |
        # | ordername  | varchar(128)   | NO   |     | NULL    |                |
        # | orderemail | varchar(256)   | NO   |     | NULL    |                |
        # | remark     | varchar(10240) | YES  |     | NULL    |                |
        # | price      | double         | NO   |     | NULL    |                |
        # | orderdate  | timestamp(6)   | NO   |     | NULL    |                |
        # +------------+----------------+------+-----+---------+----------------+
        orders_columns = ["orderid", "sellerid", "buyerid", "imgid", "ordername", "orderemail", "remark", "price",
                          "orderdate"]
        orders = db.query("orders", orders_columns)

        return orders

    def get_pv_embs_from_db(self):
        pv_records = self.get_pv_from_db()
        images = self.get_images_from_db()

        # 构建数据矩阵
        data_matrix = []

        for pv_record in pv_records:
            pv_id, viewer_id, photo_owner_id, imgid, view_timestamp = pv_record

            # 获取图片数据
            image_data = next((image for image in images if image[0] == imgid), None)
            if not image_data:
                continue

            _, _, _, imgname, story, stoneurl, price, _, likesnumber, _, _ = image_data

            data_row = (pv_id, viewer_id, photo_owner_id, imgid, view_timestamp, imgname, story, stoneurl, price, likesnumber)

            data_matrix.append(data_row)

        data_matrix_np = np.array(data_matrix)

        # 根据业务需求处理数据矩阵
        # 假设已经有了一个名为process_data_row的函数，该函数可以将data_row转换为维度为dimention的NumPy向量
        data_vectors = np.zeros((len(data_matrix_np), dimention))
        num_processes = cpu_count()

        with Pool(processes=num_processes) as pool:
            results = pool.map(process_data_row_wrapper, enumerate(data_matrix_np))

        for i, data_vector in results:
            data_vectors[i] = data_vector

        pv_index2id = {i: row[0] for i, row in enumerate(data_matrix_np)}
        pv_id2index = {row[0]: i for i, row in enumerate(data_matrix_np)}

        return pv_index2id, pv_id2index, data_vectors

    def get_images_embs_from_db(self):
        images = self.get_images_from_db()
        # 构建数据矩阵
        data_matrix = []

        for image_data in images:
            imgid, uid, orderid, imgname, story, stoneurl, price, uploaddate, likesnumber, visible, havesold = image_data

            data_row = (None, None, uid, imgid, uploaddate, imgname, story, stoneurl, price, likesnumber)

            data_matrix.append(data_row)

        data_matrix_np = np.array(data_matrix)

        # 根据业务需求处理数据矩阵
        # 假设已经有了一个名为process_data_row的函数，该函数可以将data_row转换为维度为dimention的NumPy向量
        data_vectors = np.zeros((len(data_matrix_np), dimention))
        num_processes = cpu_count()
        print(f'num_processes: {num_processes}')

        with Pool(processes=num_processes) as pool:
            results = pool.map(process_data_row_wrapper, enumerate(data_matrix_np))

        for i, data_vector in results:
            data_vectors[i] = data_vector

        img_index2id = {i: row[3] for i, row in enumerate(data_matrix_np)}
        img_id2index = {row[3]: i for i, row in enumerate(data_matrix_np)}

        return img_index2id, img_id2index, data_vectors

    def save_to_pkl(self, fdb, faiss_db_class_file_loc):
        with open(faiss_db_class_file_loc, 'wb') as file:
            pickle.dump(fdb, file)

    def load_from_pkl(self, faiss_db_class_file_loc):
        with open(faiss_db_class_file_loc, 'rb') as file:
            fdb = pickle.load(file)
        self.img_index2id, self.img_id2index, self.img_data_vectors, self.img_index = fdb

    def create_faiss_index(self, data_vectors, dimention):
        # 使用IVF和Flat索引的组合
        # nlist = 8  # 聚类中心的数量
        # quantizer = faiss.IndexFlatL2(dimention)
        # index = faiss.IndexIVFFlat(quantizer, dimention, nlist, faiss.METRIC_L2)
        # index.train(data_vectors)
        index = faiss.IndexFlatL2(dimention)
        index.add(data_vectors)
        return index

    def init_faiss_index(self):
        start_time = time.time()
        # self.pv_index2id, self.pv_id2index, self.pv_data_vectors = self.get_pv_embs_from_db()
        self.img_index2id, self.img_id2index, img_data_vectors = self.get_images_embs_from_db()
        cleaned_matrix, positions = replace_nans_infs_custom(img_data_vectors)
        print("Positions of NaN and Inf:")
        print(positions)
        self.img_data_vectors = cleaned_matrix
        self.img_index = self.create_faiss_index(self.img_data_vectors, dimention)
        fdb = [self.img_index2id, self.img_id2index, self.img_data_vectors, self.img_index]
        faiss_db_class_file_loc = self.faiss_db_class_file_loc
        # 保存为pkl
        self.save_to_pkl(fdb, faiss_db_class_file_loc)
        print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] faiss db class saved to {faiss_db_class_file_loc}')
        end_time = time.time()
        print(f'[{datetime.now().strftime("%Y-%m-%d %H:%M:%S")}] faiss db class init time: {end_time - start_time} seconds')
        pass



    def __init__(self, faiss_db_class_file_loc=None):
        self.DBController = dbControler.DBController("config.json")
        if faiss_db_class_file_loc:
            self.load_from_pkl(faiss_db_class_file_loc)
        else:
            self.faiss_db_class_file_loc = './fdb.pkl'
            self.init_faiss_index()

if __name__ == '__main__':
    # 示例：将双精度小数转换为二进制数组
    recommender = Recommender('./fdb.pkl')
    pass
