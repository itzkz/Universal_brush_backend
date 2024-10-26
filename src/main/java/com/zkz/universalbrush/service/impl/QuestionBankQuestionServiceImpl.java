package com.zkz.universalbrush.service.impl;
import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkz.universalbrush.common.ErrorCode;
import com.zkz.universalbrush.constant.CommonConstant;
import com.zkz.universalbrush.exception.ThrowUtils;
import com.zkz.universalbrush.mapper.QuestionBankQuestionMapper;
import com.zkz.universalbrush.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.zkz.universalbrush.model.entity.Question;
import com.zkz.universalbrush.model.entity.QuestionBank;
import com.zkz.universalbrush.model.entity.QuestionBankQuestion;

import com.zkz.universalbrush.model.entity.User;
import com.zkz.universalbrush.model.vo.QuestionBankQuestionVO;
import com.zkz.universalbrush.model.vo.UserVO;
import com.zkz.universalbrush.service.QuestionBankQuestionService;
import com.zkz.universalbrush.service.QuestionBankService;
import com.zkz.universalbrush.service.QuestionService;
import com.zkz.universalbrush.service.UserService;
import com.zkz.universalbrush.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库和题目关系服务实现
 *
 * @author <a href="https://github.com/itzkz">张某</a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;
    @Resource
    @Lazy
    private QuestionService questionService;
    @Resource
    private QuestionBankService questionBankService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        // 题目和题库必须存在
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQuestionQueryRequest.getId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库和题目关系封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);
//        // 2. 已登录，获取用户点赞、收藏状态
//        long questionBankQuestionId = questionBankQuestion.getId();
//        User loginUser = userService.getLoginUserPermitNull(request);
//        if (loginUser != null) {
//            // 获取点赞
//            QueryWrapper<QuestionBankQuestionThumb> questionBankQuestionThumbQueryWrapper = new QueryWrapper<>();
//            questionBankQuestionThumbQueryWrapper.in("questionBankQuestionId", questionBankQuestionId);
//            questionBankQuestionThumbQueryWrapper.eq("userId", loginUser.getId());
//            QuestionBankQuestionThumb questionBankQuestionThumb = questionBankQuestionThumbMapper.selectOne(questionBankQuestionThumbQueryWrapper);
//            questionBankQuestionVO.setHasThumb(questionBankQuestionThumb != null);
//            // 获取收藏
//            QueryWrapper<QuestionBankQuestionFavour> questionBankQuestionFavourQueryWrapper = new QueryWrapper<>();
//            questionBankQuestionFavourQueryWrapper.in("questionBankQuestionId", questionBankQuestionId);
//            questionBankQuestionFavourQueryWrapper.eq("userId", loginUser.getId());
//            QuestionBankQuestionFavour questionBankQuestionFavour = questionBankQuestionFavourMapper.selectOne(questionBankQuestionFavourQueryWrapper);
//            questionBankQuestionVO.setHasFavour(questionBankQuestionFavour != null);
//        }
        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库和题目关系封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
//        // 2. 已登录，获取用户点赞、收藏状态
//        Map<Long, Boolean> questionBankQuestionIdHasThumbMap = new HashMap<>();
//        Map<Long, Boolean> questionBankQuestionIdHasFavourMap = new HashMap<>();
//        User loginUser = userService.getLoginUserPermitNull(request);
//        if (loginUser != null) {
//            Set<Long> questionBankQuestionIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getId).collect(Collectors.toSet());
//            loginUser = userService.getLoginUser(request);
//            // 获取点赞
//            QueryWrapper<QuestionBankQuestionThumb> questionBankQuestionThumbQueryWrapper = new QueryWrapper<>();
//            questionBankQuestionThumbQueryWrapper.in("questionBankQuestionId", questionBankQuestionIdSet);
//            questionBankQuestionThumbQueryWrapper.eq("userId", loginUser.getId());
//            List<QuestionBankQuestionThumb> questionBankQuestionQuestionBankQuestionThumbList = questionBankQuestionThumbMapper.selectList(questionBankQuestionThumbQueryWrapper);
//            questionBankQuestionQuestionBankQuestionThumbList.forEach(questionBankQuestionQuestionBankQuestionThumb -> questionBankQuestionIdHasThumbMap.put(questionBankQuestionQuestionBankQuestionThumb.getQuestionBankQuestionId(), true));
//            // 获取收藏
//            QueryWrapper<QuestionBankQuestionFavour> questionBankQuestionFavourQueryWrapper = new QueryWrapper<>();
//            questionBankQuestionFavourQueryWrapper.in("questionBankQuestionId", questionBankQuestionIdSet);
//            questionBankQuestionFavourQueryWrapper.eq("userId", loginUser.getId());
//            List<QuestionBankQuestionFavour> questionBankQuestionFavourList = questionBankQuestionFavourMapper.selectList(questionBankQuestionFavourQueryWrapper);
//            questionBankQuestionFavourList.forEach(questionBankQuestionFavour -> questionBankQuestionIdHasFavourMap.put(questionBankQuestionFavour.getQuestionBankQuestionId(), true));
//        }
        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
//            questionBankQuestionVO.setHasThumb(questionBankQuestionIdHasThumbMap.getOrDefault(questionBankQuestionVO.getId(), false));
//            questionBankQuestionVO.setHasFavour(questionBankQuestionIdHasFavourMap.getOrDefault(questionBankQuestionVO.getId(), false));
        });
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

}
