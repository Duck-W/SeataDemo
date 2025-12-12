package cn.itcast.storage.web;

import cn.itcast.storage.service.StorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 虎哥
 */
@Api(tags = "库存管理接口")
@RestController
@RequestMapping("storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * 扣减库存
     * @param code 商品编号
     * @param count 要扣减的数量
     * @return 无
     */
    @ApiOperation(value = "扣减库存", notes = "根据商品编号和数量扣减库存")
    @PutMapping("/{code}/{count}")
    public ResponseEntity<Void> deduct(
            @ApiParam(name = "code", value = "商品编号", required = true) @PathVariable("code") String code,
            @ApiParam(name = "count", value = "扣减数量", required = true) @PathVariable("count") Integer count){
        storageService.deduct(code, count);
        return ResponseEntity.noContent().build();
    }
}
