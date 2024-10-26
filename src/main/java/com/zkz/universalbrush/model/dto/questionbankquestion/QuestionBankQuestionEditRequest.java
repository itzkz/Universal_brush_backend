package com.zkz.universalbrush.model.dto.questionbankquestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑题库和题目关系请求
 *
 * @author <a href="https://github.com/itzkz">张某</a>
 */
@Data
public class QuestionBankQuestionEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;


    private static final long serialVersionUID = 1L;
}