/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.core.web.controller.account;

import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.call.AddToCartItem;
import org.broadleafcommerce.core.order.service.exception.AddToCartException;
import org.broadleafcommerce.core.order.service.exception.RemoveFromCartException;
import org.broadleafcommerce.core.pricing.service.exception.PricingException;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * The controller responsible for wishlist management activities, including
 * viewing a wishlist, moving items from the wishlist to the cart, and
 * removing items from the wishlist
 *
 *
 * @author jfridye
 */
public class BroadleafManageWishlistController extends AbstractAccountController {
    
    protected static String accountWishlistView = "account/manageWishlist";
    protected static String accountWishlistRedirect = "redirect:/account/wishlist";

    public String add(HttpServletRequest request, HttpServletResponse response, Model model,
            AddToCartItem itemRequest, String wishlistName) throws IOException, AddToCartException, PricingException  {
        Order wishlist = orderService.findNamedOrderForCustomer(wishlistName, CustomerState.getCustomer(request));

        if (wishlist == null) {
            wishlist = orderService.createNamedOrderForCustomer(wishlistName, CustomerState.getCustomer(request));
        }
        
        wishlist = orderService.addItem(wishlist.getId(), itemRequest, true);
        
        return getAccountWishlistRedirect();
    }
    
    public String viewWishlist(HttpServletRequest request, HttpServletResponse response, Model model,
            String wishlistName) {
        Order wishlist = orderService.findNamedOrderForCustomer(wishlistName, CustomerState.getCustomer());
        model.addAttribute("wishlist", wishlist);
        return getAccountWishlistView();
    }

    public String removeItemFromWishlist(HttpServletRequest request, HttpServletResponse response, Model model,
            String wishlistName, Long itemId) throws RemoveFromCartException {
        Order wishlist = orderService.findNamedOrderForCustomer(wishlistName, CustomerState.getCustomer());
        
        orderService.removeItem(wishlist.getId(), itemId, false);

        model.addAttribute("wishlist", wishlist);
        return getAccountWishlistRedirect();
    }

    public String moveItemToCart(HttpServletRequest request, HttpServletResponse response, Model model, 
            String wishlistName, Long orderItemId) throws RemoveFromCartException, AddToCartException {
        Order wishlist = orderService.findNamedOrderForCustomer(wishlistName, CustomerState.getCustomer());
        List<OrderItem> orderItems = wishlist.getOrderItems();

        OrderItem orderItem = null;
        for (OrderItem item : orderItems) {
            if (orderItemId.equals(item.getId())) {
                orderItem = item;
                break;
            }
        }

        if (orderItem != null) {
            orderService.addItemFromNamedOrder(wishlist, orderItem, true);
        } else {
            throw new IllegalArgumentException("The item id provided was not found in the wishlist");
        }

        model.addAttribute("wishlist", wishlist);

        return getAccountWishlistRedirect();
    }
    
    public String moveListToCart(HttpServletRequest request, HttpServletResponse response, Model model, 
            String wishlistName) throws RemoveFromCartException, AddToCartException {
        Order wishlist = orderService.findNamedOrderForCustomer(wishlistName, CustomerState.getCustomer());
        
        orderService.addAllItemsFromNamedOrder(wishlist, true);
        
        model.addAttribute("wishlist", wishlist);
        return getAccountWishlistRedirect();
    }

    public String getAccountWishlistView() {
        return accountWishlistView;
    }
    
    public String getAccountWishlistRedirect() {
        return accountWishlistRedirect;
    }

}
