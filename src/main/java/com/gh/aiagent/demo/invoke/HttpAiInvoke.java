package com.gh.aiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;

public class HttpAiInvoke {
    public static void main(String[] args) {
        // 1. 构建请求 URL
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

        // 2. 构建请求头 (注意：此处 $DASHSCOPE_API_KEY 需替换为你的实际 API Key)
        String apiKey = TestApiKey.API_KEY;

        // 3. 构建请求体 (Body)
        // 这里直接使用 JSON 字符串，与你提供的 curl --data 内容保持一致
        String jsonBody = JSONUtil.createObj()
                .set("model", "qwen-max")
                .set("input", JSONUtil.createObj()
                        .set("messages", JSONUtil.createArray() // 创建空数组
                                .put(  // 使用 put 添加第一个对象
                                        JSONUtil.createObj()
                                                .set("role", "system")
                                                .set("content", "You are a helpful assistant.")
                                )
                                .put( // 使用 put 添加第二个对象
                                        JSONUtil.createObj()
                                                .set("role", "user")
                                                .set("content", "你是谁？")
                                )
                        )
                )
                .set("parameters", JSONUtil.createObj()
                        .set("result_format", "message")
                )
                .toString();

        // 4. 发送 POST 请求
        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey) // 添加鉴权头
                .header("Content-Type", "application/json") // 声明内容类型
                .body(jsonBody) // 设置请求体
                .timeout(20000) // 设置超时时间 (毫秒)
                .execute()) {

            // 5. 处理响应
            System.out.println("状态码: " + response.getStatus());
            System.out.println("响应内容: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}