import mysql.connector
import json


class DBController:
    def __init__(self, config_file):
        with open(config_file, "r") as file:
            config = json.load(file)
        db_config = config["db"]
        self.cnx = mysql.connector.connect(**db_config)
        self.cursor = self.cnx.cursor()

    def query(self, table, columns, where=None):
        column_names = ', '.join(columns)
        query = f"SELECT {column_names} FROM {table}"
        if where:
            query += f" WHERE {where}"
        self.cursor.execute(query)
        return self.cursor.fetchall()

    def insert(self, table, columns, values):
        column_names = ', '.join(columns)
        placeholders = ', '.join(['%s'] * len(values))
        insert_data = f"INSERT INTO {table} ({column_names}) VALUES ({placeholders})"
        self.cursor.execute(insert_data, values)
        self.cnx.commit()

    def close(self):
        self.cursor.close()
        self.cnx.close()

if __name__ == '__main__':
    db = DBController("config.json")
    print(db.query('users', ['Uid', 'username', 'password']))
    db.close()