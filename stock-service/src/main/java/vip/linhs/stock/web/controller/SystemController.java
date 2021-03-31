package vip.linhs.stock.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vip.linhs.stock.exception.FieldInputException;
import vip.linhs.stock.model.po.ExecuteInfo;
import vip.linhs.stock.model.po.SystemConfig;
import vip.linhs.stock.model.vo.CommonResponse;
import vip.linhs.stock.model.vo.PageParam;
import vip.linhs.stock.model.vo.PageVo;
import vip.linhs.stock.model.vo.TaskVo;
import vip.linhs.stock.service.RedisClient;
import vip.linhs.stock.service.SystemConfigService;
import vip.linhs.stock.service.TaskService;
import vip.linhs.stock.util.StockConsts;

@RestController
@RequestMapping("system")
public class SystemController extends BaseController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private SystemConfigService systemConfigService;

    @RequestMapping("taskList")
    public PageVo<TaskVo> getTaskList(PageParam pageParam) {
        return taskService.getAllTask(pageParam);
    }

    @PostMapping("changeTaskState")
    public CommonResponse changeTaskState(int id, int state) {
        FieldInputException e = null;
        if (state != 0 && state != 2) {
            e = new FieldInputException();
            e.addError("state", "state invalid");
        }
        if (id < 0) {
            if (e == null) {
                e = new FieldInputException();
            }
            e.addError("id", "id invalid");
        }
        if (e != null && e.hasErrors()) {
            throw e;
        }
        taskService.changeTaskState(state, id);
        CommonResponse response = CommonResponse.buildResponse("success");
        return response;
    }

    @PostMapping("executeTask")
    public CommonResponse executeTask(int id) {
        List<ExecuteInfo> list = taskService.getPendingTaskListById(id);
        for (ExecuteInfo executeInfo : list) {
            taskService.executeTask(executeInfo);
        }
        return CommonResponse.buildResponse("ok");
    }

    @RequestMapping("cacheList")
    public PageVo<Map<String, String>> getCacheList(PageParam pageParam) {
        List<Map<String, String>> list = redisClient.getAll();
        list = list.stream().filter(v -> !v.get("key").startsWith(StockConsts.CACHE_KEY_TOKEN)).collect(Collectors.toList());
        return new PageVo<>(subList(list, pageParam), list.size());
    }

    @PostMapping("deleteCache")
    public CommonResponse deleteCache(String key) {
        if (!StringUtils.hasLength(key)) {
            FieldInputException e = new FieldInputException();
            e.addError("key", "key invalid");
            throw e;
        }
        redisClient.remove(key);
        CommonResponse response = CommonResponse.buildResponse("success");
        return response;
    }

    @RequestMapping("configList")
    public PageVo<SystemConfig> getSystemConfigList(PageParam pageParam) {
        List<SystemConfig> list = systemConfigService.getAll();
        return new PageVo<>(subList(list, pageParam), list.size());
    }

}
