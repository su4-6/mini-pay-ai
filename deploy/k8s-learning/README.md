# Kubernetes 预习实验归档

> 状态：预习已归档并转交 clean 工程副本继续；本目录不构成 Day22--24 的正式完成证据。

## 已验证内容

- `hello-yaml.yaml` 中的 ConfigMap 通过 `env.valueFrom.configMapKeyRef` 注入 `App_GREETING`。
- `kubectl exec deployment/hello-yaml -- printenv App_GREETING` 曾输出 `Hello from Kubernetes ConfigMap`。
- 更新 ConfigMap 后，已有 Pod 仍保留启动时的旧环境变量；滚动重启创建的新 Pod 读取到 `Hello from updated ConfigMap`。
- 已观察 Deployment、ReplicaSet、ClusterIP Service、EndpointSlice、readiness/liveness probe 和滚动更新的基本职责。
- 归档时已用本地 YAML 解析器检查：`hello-yaml.yaml` 的 4 个文档和 `hello-gateway.yaml` 的 2 个文档均可解析；本次没有连接或改动集群。

## 尚未形成验收证据

- `hello-gateway.yaml` 是 Gateway/HTTPRoute 学习草稿，没有 `Programmed=True`、`Accepted=True` 或 `ResolvedRefs=True` 的集群输出，不能声明已部署成功。
- 本会话已确认架构边界为 `K3s Pod → host.docker.internal:13306 → Compose MySQL`，但没有完成学习者对 Wallet 连接链路的最后口述检查。
- clean 工程副本已继续形成 K3s、Compose、Gateway API 和部署脚本骨架，但当前仍是未提交、未部署状态，不能当作生产交付结果。

## 后续边界

- Day22--24 仍按冻结计划完成一个无状态服务的部署、配置、探针、Service、入口、升级、回滚和日志排障闭环。
- MySQL、Redis、RabbitMQ、Seata 等有状态基础设施继续由 Compose 承载；Kubernetes 不直接管理这些容器。
- 本目录不再继续扩展完整 MiniPay 部署；后续工程改造以 clean 副本中的部署蓝图和实际验证为准。
