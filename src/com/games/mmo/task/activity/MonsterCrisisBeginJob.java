package com.games.mmo.task.activity;

import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import com.games.mmo.cache.GlobalCache;
import com.games.mmo.po.CopySceneActivityPo;
import com.games.mmo.po.GlobalPo;
import com.games.mmo.po.RolePo;
import com.games.mmo.service.ChatService;
import com.games.mmo.type.ColourType;
import com.games.mmo.type.CopySceneType;
import com.games.mmo.vo.RetrieveSystemVo;
import com.storm.lib.template.RoleTemplate;
import com.storm.lib.util.BeanUtil;
import com.storm.lib.util.DateUtil;
import com.storm.lib.util.ExceptionUtil;
import com.storm.lib.util.PrintUtil;
import com.storm.lib.util.StringUtil;

/**
 * 魔化危机开始
 * @author Administrator
 *
 */
public class MonsterCrisisBeginJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			beginMonsterCrisisContext(context);
		} catch (Exception e) {
			ExceptionUtil.processException(e);
		}
	}

	public void beginMonsterCrisisContext(JobExecutionContext context){
		Trigger trigger = context.getTrigger();
		Date date = trigger.getNextFireTime();
		String  jobName = context.getJobDetail().getName();		
		List<String> list = StringUtil.getStringListByStr(jobName, "_");	
		int id = Integer.parseInt(list.get(1));
		long nextTime =0l;
		if(date != null){
			nextTime = date.getTime();
		}
		beginMonsterCrisisDispose(id, nextTime);
	}
	
	public void beginMonsterCrisisDispose(Integer id, Long nextTime){
		CopySceneActivityPo copySceneActivityPo = GlobalCache.mapCopySceneActivityPos.get(id);
		copySceneActivityPo.setActivityWasOpen(1);
		copySceneActivityPo.setBeginTime(System.currentTimeMillis());
		copySceneActivityPo.setBeginTimeNext(nextTime);
		
		GlobalPo gp = (GlobalPo) GlobalPo.keyGlobalPoMap.get(GlobalPo.keyAwardRetrieve);
		List<RetrieveSystemVo> listRetrieveSystemVo = (List<RetrieveSystemVo>) gp.valueObj;
		for(RetrieveSystemVo retrieveSystemVo : listRetrieveSystemVo){
			if(retrieveSystemVo.id.intValue() == CopySceneType.COPY_SCENE_CONF_MONSTERCRISIS){
				retrieveSystemVo.time=DateUtil.getInitialDate(System.currentTimeMillis());
				retrieveSystemVo.todayState=1;
				break;
			}
		}
		PrintUtil.print("魔化危机开启:"+DateUtil.getFormatDateBytimestamp(System.currentTimeMillis()));
		PrintUtil.print("listRetrieveSystemVo="+listRetrieveSystemVo);
		
		StringBuffer sb = new StringBuffer();
		sb.append(ColourType.COLOUR_WHITE).append(GlobalCache.fetchLanguageMap("key250"));
		ChatService chatService=(ChatService) BeanUtil.getBean("chatService");
		chatService.sendHorse(sb.toString());
		chatService.sendSystemWorldChat(sb.toString());
		
//		PrintUtil.print("魔化危机 开始：" +new Date().toLocaleString()+" " +copySceneActivityPo);
		for(Integer roleId : RoleTemplate.roleIdIuidMapping.keySet()){
			RolePo rolePo = RolePo.findEntity(roleId);
			rolePo.sendUpdateCopySceneAativityWasOpenInfo();
		}
	}
}
