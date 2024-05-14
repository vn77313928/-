import weka.clusterers.ClusterEvaluation;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class KMeansClustering {

    public static void main(String[] args) throws Exception {
        // 1. 从数据库加载数据
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("Address"));
        attributes.add(new Attribute("ProductCategory"));
        attributes.add(new Attribute("TotalExpenditure"));

        Instances data = new Instances("UserProfiles", attributes, 0);

        // 连接数据库
        String url = "jdbc:mysql://localhost:3306/your_database";
        String user = "your_username";
        String password = "your_password";
        Connection connection = DriverManager.getConnection(url, user, password);

        // 查询数据库
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM user_profiles");

        // 添加实例（从数据库中获取）
        while (resultSet.next()) {
            Instance instance = new DenseInstance(3);
            instance.setValue(attributes.get(0), resultSet.getString("address"));
            instance.setValue(attributes.get(1), resultSet.getString("product_category"));
            instance.setValue(attributes.get(2), resultSet.getDouble("total_expenditure"));
            data.add(instance);
        }

        // 关闭连接
        resultSet.close();
        statement.close();

        //选择聚类数为5
        int k = 5; 

      构建K-means聚类模型
        SimpleKMeans kMeans = new SimpleKMeans();
        kMeans.setNumClusters(k);
        kMeans.buildClusterer(data);



        // 获取聚类结果
        ClusterEvaluation eval = new ClusterEvaluation();
        eval.setClusterer(kMeans);
        eval.evaluateClusterer(data);

        // 分析和解释聚类结果
        double[] assignments = eval.getClusterAssignments();
        for (int i = 0; i < assignments.length; i++) {
            System.out.println("Instance " + i + " is in cluster " + (int) assignments[i]);
        }

      

        // 输出数据
        // 将聚类结果保存为ARFF文件
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File("user_profiles.arff"));
        saver.writeBatch();
    }
}