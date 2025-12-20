package com.dang.orderservice.application.port;

import com.dang.sagamessages.message.inventory.InventoryCommands;


public interface InventoryCommandPort {
    void sendReserveInventory(InventoryCommands.ReserveInventoryCommand command);
    void sendReleaseInventory(InventoryCommands.ReleaseInventoryCommand command);
}
