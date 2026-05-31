package com.gh.aiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");

        try {
            // 1. 发送请求
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);

            // 2. 使用 Hutool 解析 JSON (修复了类型不匹配问题)
            JSONObject jsonObject = JSONUtil.parseObj(response);

            // 3. 获取数组 (Hutool 的 getJSONArray 返回 cn.hutool.json.JSONArray)
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");

            // 4. 安全检查：防止空指针和索引越界
            if (organicResults == null || organicResults.isEmpty()) {
                return "No results found.";
            }

            // 5. 截取前5条数据 (Math.min 防止结果不足5条时报错)
            int end = Math.min(organicResults.size(), 2);
            List<Object> objects = organicResults.subList(0, end);

            // 6. 流式处理
            String result = objects.stream().map(obj -> {
                // Hutool 的类型强转
                JSONObject tmpJSONObject = (JSONObject) obj;
                return tmpJSONObject.toString();
            }).collect(Collectors.joining(","));

            return result;

        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}