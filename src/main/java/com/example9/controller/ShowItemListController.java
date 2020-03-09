package com.example9.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example9.domain.Item;
import com.example9.form.SortConditionNumberForm;
import com.example9.service.ShowItemListService;

/**
 * 商品を一覧表示するコントローラーです.
 * 
 * @author mizuki
 *
 */
@Controller
@RequestMapping("/")
public class ShowItemListController {

	@Autowired
	private ShowItemListService showItemListService;

	@Autowired
	private HttpSession session;

	@Autowired
	private ServletContext application;

	@ModelAttribute
	public SortConditionNumberForm setUpSortConditionNumberForm() {
		SortConditionNumberForm form = new SortConditionNumberForm();
		form.setSortConditionNumber("0");
		return form;
	}

	/**
	 * 商品一覧表示を行います.
	 * 
	 * @param model リクエストスコープ
	 * @return 商品一覧画面
	 */
	@RequestMapping("")
	public String showList(Model model, Integer pagingNumber) {

		List<Item> itemList = showItemListService.showList();

		// 変数pageにページ数を格納する
		int pageNumber = 0;
		if (itemList.size() % 6 == 0) {
			pageNumber = itemList.size() / 6;
		} else if (itemList.size() % 6 != 0) {
			pageNumber = (itemList.size() / 6) + 1;
		}
		// リストにページ数を入れて、リクエストスコープに格納する
		List<Integer> pageList = new ArrayList<>();
		for (int i = 1; i <= pageNumber; i++) {
			pageList.add(i);
		}
		model.addAttribute("pageList", pageList);

		System.out.println(pagingNumber);
		if (pagingNumber == null) {
			itemList = showItemListService.ShowListpaging(1);
		} else {
			itemList = showItemListService.ShowListpaging(6 * (pagingNumber - 1));
			System.out.println(itemList);
		}

		// 並び替え用にsessionスコープに残しておく
		session.setAttribute("itemList", itemList);

		// オートコンプリート用の記述
		List<Item> fullItemList = showItemListService.showList();
		StringBuilder itemListForAutocomplete = showItemListService.getItemListForAutocomplete(fullItemList);
		application.setAttribute("itemListForAutocomplete", itemListForAutocomplete);

		List<List<Item>> itemListList = getThreeItemList(itemList);

		model.addAttribute("itemListList", itemListList);
		return "item_list_curry";
	}

	/**
	 * 商品の曖昧検索を行います.
	 * 
	 * @param name  名前
	 * @param model リクエストスコープ
	 * @return 商品一覧画面
	 */
	@RequestMapping("/searchResult")
	public String serchByLikeName(String code, Model model, Integer pagingNumber) {
		List<Item> itemList = showItemListService.searchByLikeName(code);

		if (itemList.size() == 0) {
			String message = "該当する商品がありません";
			model.addAttribute("message", message);
			return showList(model, pagingNumber);
		} else {
			// 並び替え用にsessionスコープに残しておく
			session.setAttribute("itemList", itemList);
			List<List<Item>> itemListList = getThreeItemList(itemList);
			model.addAttribute("itemListList", itemListList);
		}
		return "item_list_curry";
	}

	@RequestMapping("/sortShowList")
	public String sortShowList(SortConditionNumberForm form, Model model) {
		@SuppressWarnings("unchecked")
		List<Item> itemList = (List<Item>) session.getAttribute("itemList");
		itemList = sortItemList(itemList, form);
		List<List<Item>> itemListList = getThreeItemList(itemList);
		model.addAttribute("itemListList", itemListList);
		return "item_list_curry";
	}

	/**
	 * ItemListをソートする.
	 * 
	 * @param itemList ソートしたいItemList
	 * @param form     並び替えフォーム
	 * @return ソート済みのitemList
	 */
	private List<Item> sortItemList(List<Item> itemList, SortConditionNumberForm form) {
		Comparator<Item> sortCondition = null;

		if ("0".equals(form.getSortConditionNumber())) {
			// Mサイズの価格の昇順でソートするComparator
			sortCondition = new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					return item1.getPriceM().compareTo(item2.getPriceM());
				}
			};
		} else if ("1".equals(form.getSortConditionNumber())) {
			// Mサイズの価格の降順でソートするComparator
			sortCondition = new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					return item2.getPriceM().compareTo(item1.getPriceM());
				}
			};
		} else if ("2".equals(form.getSortConditionNumber())) {
			// 口コミ評価点数の降順でソートするComparator
			sortCondition = new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					return item2.getAveEvaluationTenfold().compareTo(item1.getAveEvaluationTenfold());
				}
			};
		} else if ("3".equals(form.getSortConditionNumber())) {
			// 口コミ件数の降順でソートするComparator
			sortCondition = new Comparator<Item>() {
				@Override
				public int compare(Item item1, Item item2) {
					return item2.getCountEvaluation().compareTo(item1.getCountEvaluation());
				}
			};
		}

		Collections.sort(itemList, sortCondition);
		return itemList;
	}

	/**
	 * 3個のItemオブジェクトを1セットにしてリストで返す.
	 * 
	 * @param itemList 商品リスト
	 * @return 3個1セットの商品リスト
	 */
	private List<List<Item>> getThreeItemList(List<Item> itemList) {
		List<List<Item>> itemListList = new ArrayList<>();
		List<Item> threeItemList = new ArrayList<>();

		for (int i = 1; i <= itemList.size(); i++) {
			threeItemList.add(itemList.get(i - 1));

			if (i % 3 == 0 || i == itemList.size()) {
				itemListList.add(threeItemList);
				threeItemList = new ArrayList<>();
			}
		}
		return itemListList;
	}

}
