package br.com.casadocodigo.loja.controllers;

import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.casadocodigo.loja.daos.ProductDAO;
import br.com.casadocodigo.loja.models.BookType;
import br.com.casadocodigo.loja.models.Product;

@Controller
@RequestMapping("/products")
public class ProductsController {
	
	@Autowired
	private ProductDAO productDAO;
	
	@Autowired
	private FileSaver fileSaver;

	@RequestMapping(value= "/form", method=RequestMethod.GET)
	public ModelAndView form(Product product) {
		ModelAndView modelAndView = new ModelAndView("products/form");
		modelAndView.addObject("types", BookType.values());
		return modelAndView;
	}
	
	@CacheEvict(value="lastProducts")
	@Transactional
	@RequestMapping(method= RequestMethod.POST, name="saveProduct")
	public ModelAndView save(MultipartFile summary, @Valid Product product, BindingResult bindingResults, RedirectAttributes redirectAttributes){
		if(bindingResults.hasErrors()){
			return this.form(product);
		}
		String webPath = fileSaver.write("upload-summaries",summary);
		product.setSummaryPath(webPath);
		
		System.out.println("Cadastrando o produto: " + product);
		productDAO.save(product);
		redirectAttributes.addFlashAttribute("sucesso", "Produto cadastrado com sucesso");
		return new ModelAndView("redirect:products");
	}
	
	@RequestMapping(method=RequestMethod.GET)
	@Cacheable(value="lastProducts")
	public ModelAndView list(){
		List<Product> products = productDAO.list();
		products.forEach(p -> System.out.println(p));
		ModelAndView modelAndView = new ModelAndView("products/list");
		modelAndView.addObject("products", products);
		return modelAndView;
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/{id}")
	public ModelAndView show(@PathVariable("id") Integer id){
		ModelAndView modelAndView = new ModelAndView("products/show");
		modelAndView.addObject("product", productDAO.find(id));
		return modelAndView;
	}
	
	/* Não se faz mais necessario pois o HibernateValidations vai passar a validar através das anotações em Produtc
	@InitBinder
	public void initBinder(WebDataBinder binder){
		binder.addValidators(new ProductValidator());
	}
	*/

}
