/*
 * This file is part of the LibreOffice project.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

#import <Foundation/Foundation.h>
#import "Server.h"
#import "CommunicationManager.h"
#import "CommandInterpreter.h"

@interface Client : NSObject

@property BOOL ready;
@property (nonatomic, strong) NSNumber* pin;
@property (nonatomic, strong) NSString* name;

-(void) connect;

- (id) initWithServer:(Server*)server
            managedBy:(CommunicationManager*)manager
        interpretedBy:(CommandInterpreter*)receiver;

- (void) sendCommand:(NSString *)aCommand;

-(void)stream:(NSStream *)stream handleEvent:(NSStreamEvent)eventCode;

@end