package com.panqide.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @title: TODO
 * @description: TODO
 * @date: 2020/8/27 12:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Content {
    private String title;
    private String price;
    private String img;

}
