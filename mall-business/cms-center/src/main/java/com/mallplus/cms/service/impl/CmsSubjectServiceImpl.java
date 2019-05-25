package com.mallplus.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mallplus.common.entity.cms.CmsSubject;
import com.mallplus.common.entity.cms.CmsSubjectCategory;
import com.mallplus.common.entity.cms.SmsHomeRecommendSubject;
import com.mallplus.cms.mapper.CmsSubjectCategoryMapper;
import com.mallplus.cms.mapper.CmsSubjectMapper;
import com.mallplus.cms.service.ICmsSubjectService;
import com.mallplus.cms.service.ISmsHomeRecommendSubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 专题表 服务实现类
 * </p>
 *
 * @author zscat
 * @since 2019-04-17
 */
@Service
public class CmsSubjectServiceImpl extends ServiceImpl<CmsSubjectMapper, CmsSubject> implements ICmsSubjectService {
    @Resource
    private CmsSubjectMapper subjectMapper;
    @Resource
    private ISmsHomeRecommendSubjectService homeRecommendSubjectService;


    @Resource
    private CmsSubjectCategoryMapper subjectCategoryMapper;

    @Override
    @Transactional
    public boolean saves(CmsSubject entity) {
        entity.setCreateTime(new Date());
        subjectMapper.insert(entity);
        CmsSubjectCategory category = subjectCategoryMapper.selectById(entity.getCategoryId());
        category.setSubjectCount(category.getSubjectCount() + 1);
        subjectCategoryMapper.updateById(category);
        return true;
    }

    @Override
    public int updateRecommendStatus(Long ids, Integer recommendStatus) {
        CmsSubject record = new CmsSubject();
        record.setRecommendStatus(recommendStatus);
        return subjectMapper.update(record, new QueryWrapper<CmsSubject>().in("id",ids));
    }

    @Override
    public int updateShowStatus(Long ids, Integer showStatus) {
        CmsSubject record = new CmsSubject();
        record.setShowStatus(showStatus);
        return subjectMapper.update(record, new QueryWrapper<CmsSubject>().in("id",ids));
    }

    @Override
    public List<CmsSubject> getRecommendSubjectList(int pageNum, int pageSize) {
        List<SmsHomeRecommendSubject> brands = homeRecommendSubjectService.list(new QueryWrapper<>());
        List<Long> ids = brands.stream()
                .map(SmsHomeRecommendSubject::getId)
                .collect(Collectors.toList());
        return (List<CmsSubject>) subjectMapper.selectBatchIds(ids);
    }
}
