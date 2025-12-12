package cn.itcast.order.web;

import cn.itcast.order.entity.Order;
import cn.itcast.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 虎哥
 */
@Api(tags = "订单管理接口")
@RestController
@RequestMapping("order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApiOperation(value = "创建订单", notes = "创建一个新的订单")
    @PostMapping
    public ResponseEntity<Long> createOrder(
            @ApiParam(name = "order", value = "订单信息", required = true) @RequestBody Order order){
        Long orderId = orderService.create(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }
}
