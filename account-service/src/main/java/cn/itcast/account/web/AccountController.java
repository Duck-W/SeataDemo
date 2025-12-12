package cn.itcast.account.web;

import cn.itcast.account.service.AccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 虎哥
 */
@Api(tags = "账户管理接口")
@RestController
@RequestMapping("account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @ApiOperation(value = "扣减账户余额", notes = "根据用户ID和金额扣减账户余额")
    @PutMapping("/{userId}/{money}")
    public ResponseEntity<Void> deduct(
            @ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable("userId") String userId, 
            @ApiParam(name = "money", value = "扣减金额", required = true) @PathVariable("money") Integer money){
        accountService.deduct(userId, money);
        return ResponseEntity.noContent().build();
    }
}
