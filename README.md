# Mu Smart Home

![Mu Smart Home](img/MuSmartHome.png)

##Instructions

You need to export the environment variable `GOOGLE_APPLICATION_CREDENTIALS`. It must point to the JSON file that contains your service account key.

Then, you can deploy a kubernetes cluster with `mu-smart-home` server running on it with `gcloud deployment-manager deployments create mu-example --config cloudbuild.yaml`. Also, you will deploy a bigquery table and a pubsub topic. You can modify the configuration in the `cloudbuild.yaml` configuration file 