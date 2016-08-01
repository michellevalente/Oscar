//
//  Command.swift
//  Oscar
//
//  Created by Michelle Valente on 29/07/16.
//  Copyright © 2016 Michelle Valente. All rights reserved.
//

import UIKit

class Command: NSObject {
    
    var children = [Command]()
    var type = String()
    var param = Int()
    
    init(type:String, param: Int) {
        self.type = type
        self.param = param
    }
    
    convenience init(type:String) {
     self.init(type: type, param: -1)
    }
    
    func addCommand(c : Command) -> Bool {
        if (type == "loop" && !(c.type == "loop")) {
            children.append(c)
            return true
        }
        return false;
    }
    
    func removeCommand(index:Int) -> Bool {
        if (index > children.count){
            return false;
        }
        children.removeAtIndex(index);
        return true;
    }
    
    
    func serialize() -> String{
        var str = self.type
        if(type == "loop")
        {

            for (_,c) in children.enumerate() {
                str += c.toString()
            }
        }
        else if (param != -1)
        {
            str += String(param)
        }
        
        return str
    }
    
    func toString() -> String {
        var str = self.type + " "
        
        if(type == "move"){
            if(param == 1)
            {
                str += "forward"
            }
            else if(param == 0)
            {
                str += "backward"
            }
        }
        else if(type == "turn")
        {
            if(param == 1)
            {
                str += "right"
            }
            else if(param == 0)
            {
                str += "left"
            }
        }
        else if(type.lowercaseString.rangeOfString("led") != nil)
        {
            if(type == "ledr")
            {
                str = "red led"
            }
            else if(type == "ledb")
            {
                str = "blue led"
            }
            
            str += " "
            
            if(param == 1)
            {
                str += "on"
            }
            else if(param == 0)
            {
                str += "off"
            }
        }
        else if(type == "buzz" || type == "wait")
        {
            str += String(param) + "ms"
        }
        
        return str
        
    }
}
