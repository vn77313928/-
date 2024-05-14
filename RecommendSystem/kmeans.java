import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class KMeansClustering {

    public static void main(String[] args) {
        try {
             1. 加载数据集
            DataSource source = new DataSource(user_data.arff);
            Instances data = source.getDataSet();

             2. 构建K-means聚类模型
            SimpleKMeans kMeans = new SimpleKMeans();
            kMeans.setNumClusters(3);  设置聚类数目为3，可以根据实际情况调整
            kMeans.buildClusterer(data);

             3. 获取聚类结果
            int[] clusterAssignments = kMeans.getAssignments();  获取每个实例的聚类标签
            Instances centroids = kMeans.getClusterCentroids();  获取每个聚类的中心点

             4. 分析和解释聚类结果
            for (int i = 0; i  centroids.numInstances(); i++) {
                System.out.println(Cluster  + (i+1) +  Center  + centroids.instance(i));
            }

             5. 生成用户画像
            for (int i = 0; i  data.numInstances(); i++) {
                int cluster = clusterAssignments[i];  获取当前实例的聚类标签
                System.out.println(Instance  + (i+1) +  belongs to Cluster  + (cluster+1));
                 根据需要，进一步解析实例特征，生成用户画像
                 例如，获取实例的地址、购买商品种类、总消费等信息，然后输出用户画像
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}