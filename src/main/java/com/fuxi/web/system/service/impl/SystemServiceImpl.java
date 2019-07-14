package com.fuxi.web.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fuxi.core.common.service.impl.CommonServiceImpl;
import com.fuxi.web.system.service.SystemService;

@Service("systemService")
@Transactional
public class SystemServiceImpl extends CommonServiceImpl implements SystemService {



}
