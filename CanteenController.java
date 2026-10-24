package com.college.canteen;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class CanteenController {
    public static class FoodItem {
        public int id;
        public String name;
        public double price;
        public int quantity;
        public FoodItem() {}
        FoodItem(int id, String name, double price, int quantity) {
            this.id=id; this.name=name; this.price=price; this.quantity=quantity;
        }
    }
    public static class OrderLine {
        public int itemId;
        public int quantity;
    }
    public static class OrderRequest {
        public List<OrderLine> items = new ArrayList<>();
    }
    public static class Order {
        public String token;
        public List<Map<String,Object>> items = new ArrayList<>();
        public double total;
        public String status = "Pending";
        public String createdAt = new Date().toString();
    }

    private final Map<Integer,FoodItem> foods = new LinkedHashMap<>();
    private final Map<String,Order> orders = new LinkedHashMap<>();
    private final AtomicInteger itemIds = new AtomicInteger(4);
    private final AtomicInteger tokenIds = new AtomicInteger(1001);

    public CanteenController() {
        foods.put(1,new FoodItem(1,"Veg Sandwich",45,20));
        foods.put(2,new FoodItem(2,"Masala Dosa",60,15));
        foods.put(3,new FoodItem(3,"Lemon Rice",40,25));
        foods.put(4,new FoodItem(4,"Tea",12,50));
    }

    @GetMapping("/items")
    public synchronized Collection<FoodItem> getItems() { return new ArrayList<>(foods.values()); }

    @PostMapping("/items")
    public synchronized FoodItem addItem(@RequestBody FoodItem input) {
        if (input.name == null || input.name.isBlank() || input.price < 0 || input.quantity < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a name, valid price and quantity.");
        FoodItem item = new FoodItem(itemIds.incrementAndGet(),input.name.trim(),input.price,input.quantity);
        foods.put(item.id,item);
        return item;
    }

    @PutMapping("/items/{id}")
    public synchronized FoodItem updateItem(@PathVariable int id,@RequestBody FoodItem input) {
        FoodItem item=foods.get(id);
        if(item==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Food item not found.");
        if(input.name==null || input.name.isBlank() || input.price<0 || input.quantity<0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter valid item details.");
        item.name=input.name.trim(); item.price=input.price; item.quantity=input.quantity;
        return item;
    }

    @PostMapping("/orders")
    public synchronized Order placeOrder(@RequestBody OrderRequest request) {
        if(request.items==null || request.items.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Select at least one food item.");
        Map<Integer,Integer> quantities = new LinkedHashMap<>();
        for(OrderLine line:request.items) {
            if(line.quantity<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantity must be at least 1.");
            quantities.merge(line.itemId,line.quantity,Integer::sum);
        }
        for(var entry:quantities.entrySet()) {
            FoodItem food=foods.get(entry.getKey());
            if(food==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown food item.");
            if(food.quantity<entry.getValue())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Not enough stock for "+food.name+". Available: "+food.quantity);
        }
        Order order=new Order();
        order.token="CT-"+tokenIds.getAndIncrement();
        for(var entry:quantities.entrySet()) {
            FoodItem food=foods.get(entry.getKey());
            int qty=entry.getValue();
            food.quantity-=qty;
            double subtotal=food.price*qty;
            Map<String,Object> row=new LinkedHashMap<>();
            row.put("itemId",food.id); row.put("name",food.name); row.put("quantity",qty);
            row.put("price",food.price); row.put("subtotal",subtotal);
            order.items.add(row); order.total+=subtotal;
        }
        orders.put(order.token,order);
        return order;
    }

    @GetMapping("/orders")
    public synchronized Collection<Order> getOrders() { return new ArrayList<>(orders.values()); }

    @PutMapping("/orders/{token}/status")
    public synchronized Order updateStatus(@PathVariable String token,@RequestBody Map<String,String> body) {
        Order order=orders.get(token);
        if(order==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order token not found.");
        String status=body.get("status");
        if(!Set.of("Pending","Preparing","Completed").contains(status))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Status must be Pending, Preparing or Completed.");
        order.status=status;
        return order;
    }
}
