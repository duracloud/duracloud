/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

/**
 * 
 * @author Daniel Bernstein
 */
;(function(dc){
    if(dc == undefined){
    	dc = {};
    }

    if(dc.util == undefined){
        	dc.util = {};
    }	
	
	var State = {
		NEW: "new",
		CANCELLING:"cancelling",
		CANCELLED: "cancelled",
		RUNNING:"running",
		DONE: "done",
	};


	dc.util.createJob = function(jobId){
		var workerCount = 10;
		var taskQueue = new Array();
		var reducer = null;
		var activeWorkers = 0;
		var cancel = false;
		
		var job =  {
			
			getState: function(){
				
				var state = State.NEW;
				if(reducer != null){
					if(cancel){
						if(activeWorkers == 0){
							state = State.CANCELLED;
						}else{
							state = State.CANCELLING;
						}
					}else{
						if(activeWorkers == 0){
							state = State.DONE;
						}else{
							state = State.RUNNING;
						}
					}
				}
				
				return state;
			},
			
			getProgress: function(){
				if(reducer != null){
					return {
						taskCount: reducer.taskCount,
						successes: reducer.successes,
						failures: reducer.failures,
					};
				}else{
					return {};
				}
			},
				
			toString: function(){
				var p = this.getProgress();
				
				return "Job(id="+this.getJobId() +", state: " + this.getState() + 
						", successes: " + p.successes + ", failures:" + p.failures + ")";
			},
			
			getJobId: function(){
				return jobId;
		    },
		    
			addTask: function(task){
				taskQueue.push(task);
			},
			
			_processNextTask: function(reducer){
				var task = taskQueue.shift();
				if(task != null){
					activeWorkers++;
					task.execute(reducer);
				}
			},
			
			execute: function(callback){
				var that = this;
				if(reducer != null){
					throw "Already executed!";
				}
				
				reducer = {
					taskCount: taskQueue.length,  	
					successes: 0,
					failures: 0,
					success: function(task){
						this.successes++;
						callback.changed(job);
						that._processNextTask(this);
						this._decrementActiveWorkers(callback);
					},
					
					_decrementActiveWorkers: function(callback){
						activeWorkers--;
						if(activeWorkers == 0){
							if(cancel){
								callback.cancelled(job);
							}else{
								callback.done(job);
							}
						}
					},
					
					failure: function(task){
						this.failures++;
						callback.changed(job);
						that._processNextTask(this);
						this._decrementActiveWorkers(callback);
					},
				};
				
				var i;
				for(i = 0; i < workerCount; i++){
					that._processNextTask(reducer);
				}
			},
			
			cancel: function(callback){
				if(taskQueue.length > 0){
					if(!cancel){
						taskQueue.clear();
					}
				}
			},
		};
		
		return job;
		
	};	
})(dc);
