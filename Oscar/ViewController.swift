//
//  ViewController.swift
//  Oscar
//
//  Created by Michelle Valente on 28/07/16.
//  Copyright © 2016 Michelle Valente. All rights reserved.
//


// LOOK FOR BTDISCOVERY

import UIKit

class ViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {

    @IBOutlet weak var AddCommandButton: UIButton!
    @IBOutlet weak var SendButton: UIButton!
    @IBOutlet weak var CleanButton: UIButton!
    @IBOutlet var tableView: UITableView!
    @IBOutlet weak var TabButtons: UISegmentedControl!
    
    var commandList = Dictionary<String, Array<Command>>()
    var currentTab = "Main"
    
    var typeCommand = ""
    var typeParam = ""
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.commandList[currentTab]!.count;
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell:UITableViewCell = self.tableView.dequeueReusableCellWithIdentifier("cell")! as UITableViewCell
        
        cell.textLabel?.text = commandList[currentTab]![indexPath.row].toString()
        
        return cell
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        commandList["Main"] = []
        commandList["Timer"] = []
        commandList["Sensor"] = []
        // Do any additional setup after loading the view, typically from a nib.
        self.tableView.registerClass(UITableViewCell.self, forCellReuseIdentifier: "cell")
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    func saveCommand(action: UIAlertAction, _ alert:UIAlertController, param:String) {
        
        let newCommand = Command(type: typeCommand, param: Int(param)!)

        commandList[currentTab]!.append(newCommand)
        
        self.tableView.reloadData()
    }
    
    func addParam(action: UIAlertAction, _ alert:UIAlertController, command:String)  {
        
        typeCommand = command
        
        if(action.title == "Loop" || action.title == "Stop")
        {
            let newCommand = Command(type: typeCommand)
            commandList[currentTab]!.append(newCommand)
            self.tableView.reloadData()
            return
        }
        
        let alert = UIAlertController(title: "Add Parameters", message: nil, preferredStyle: UIAlertControllerStyle.Alert)
        
        if(action.title == "Red Led" || action.title == "Blue Led")
        {
            alert.addAction(UIAlertAction(title:"On", style:.Default, handler: {
                action in self.saveCommand(action, alert, param: "1")
            }))
            alert.addAction(UIAlertAction(title:"Off", style:.Default, handler: {
                action in self.saveCommand(action, alert, param: "0")
            }))
        }
        
        if(action.title == "Move")
        {
            alert.addAction(UIAlertAction(title:"Forward", style:.Default, handler: {
                action in self.saveCommand(action, alert, param: "1")
            }))
            alert.addAction(UIAlertAction(title:"Backward", style:.Default, handler: {
                action in self.saveCommand(action, alert, param: "0")
            }))
        }
            
        if(action.title == "Turn")
        {
            alert.addAction(UIAlertAction(title:"Right", style:.Default, handler: {
                action in self.saveCommand(action, alert, param: "1")
            }))
            alert.addAction(UIAlertAction(title:"Left", style:.Default, handler: {
                action in self.saveCommand(action, alert, param: "0")
            }))
        }
            
        else if(action.title == "Wait" || action.title == "Buzz")
        {
            alert.addTextFieldWithConfigurationHandler { (textField) in
                textField.placeholder = "Time"
                textField.keyboardType = .NumberPad
            }
            
            alert.addAction(UIAlertAction(title:"Ok", style:.Default, handler: {
                action in self.saveCommand(action, alert, param: alert.textFields![0].text!)
            }))
        }
        
        // show the alert
        self.presentViewController(alert, animated: true, completion: nil)
        
    }
    
    @IBAction func AddCommandAction(sender: AnyObject) {
        // create the alert
        let alert = UIAlertController(title: "Add Command", message: "Choose what type of command you want Oscar to do", preferredStyle: UIAlertControllerStyle.Alert)
        
        // add the actions (buttons)
        alert.addAction(UIAlertAction(title:"Loop", style:.Default, handler: {
            action in self.addParam(action, alert, command: "loop")
        }))
        alert.addAction(UIAlertAction(title:"Red Led", style:.Default, handler: {
            action in self.addParam(action, alert, command:"ledr")
        }))
        alert.addAction(UIAlertAction(title:"Blue Led", style:.Default, handler: {
            action in self.addParam(action, alert, command:"ledb")
        }))
        alert.addAction(UIAlertAction(title:"Move", style:.Default, handler: {
            action in self.addParam(action, alert, command:"move")
        }))
        alert.addAction(UIAlertAction(title:"Turn", style:.Default, handler: {
            action in self.addParam(action, alert, command:"turn")
        }))
        alert.addAction(UIAlertAction(title:"Buzz", style:.Default, handler: {
            action in self.addParam(action, alert, command:"buzz")
        }))
        alert.addAction(UIAlertAction(title:"Wait", style:.Default, handler: {
            action in self.addParam(action, alert, command:"wait")
        }))
        alert.addAction(UIAlertAction(title:"Stop", style:.Default, handler: {
            action in self.addParam(action, alert, command:"stop")
        }))
        
        // show the alert
        self.presentViewController(alert, animated: true, completion: nil)
    }

    @IBAction func CleanCode(sender: AnyObject) {
        commandList[currentTab]!.removeAll()
        self.tableView.reloadData()
    }

    @IBAction func SendCode(sender: AnyObject) {
        
        for command in commandList["Main"]!{
            print(command.serialize())
        }
    }
    @IBAction func ChangeTab(sender: AnyObject) {
        currentTab = TabButtons.titleForSegmentAtIndex(TabButtons.selectedSegmentIndex)!
        self.tableView.reloadData()
    }
}

